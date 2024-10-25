// src/components/registration/RegistrationForm.jsx
import { useState } from 'react';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import axios from 'axios';
import { toast } from 'react-toastify';

const validationSchema = Yup.object({
  studentId: Yup.string()
    .required('Vui lòng nhập mã sinh viên')
    .matches(/^[0-9]+$/, 'Mã sinh viên chỉ được chứa số'),
  fullName: Yup.string()
    .required('Vui lòng nhập họ và tên')
    .min(2, 'Tên phải có ít nhất 2 ký tự'),
  email: Yup.string()
    .required('Vui lòng nhập email')
    .email('Email không hợp lệ')
});

const RegistrationForm = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (values, { resetForm }) => {
    try {
      setIsSubmitting(true);
      await axios.post('/api/registrations/request', values);
      toast.success('Đăng ký thành công! Vui lòng chờ phê duyệt.');
      resetForm();
    } catch (error) {
      const message = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.';
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow-md">
      <h2 className="text-2xl font-bold mb-6 text-center">Đăng Ký Tài Khoản Thư Viện</h2>
      
      <Formik
        initialValues={{ studentId: '', fullName: '', email: '' }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        {({ errors, touched }) => (
          <Form className="space-y-4">
            <div>
              <label htmlFor="studentId" className="block text-sm font-medium text-gray-700">
                Mã sinh viên
              </label>
              <Field
                name="studentId"
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
              />
              {errors.studentId && touched.studentId && (
                <div className="text-red-500 text-sm mt-1">{errors.studentId}</div>
              )}
            </div>

            <div>
              <label htmlFor="fullName" className="block text-sm font-medium text-gray-700">
                Họ và tên
              </label>
              <Field
                name="fullName"
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
              />
              {errors.fullName && touched.fullName && (
                <div className="text-red-500 text-sm mt-1">{errors.fullName}</div>
              )}
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email
              </label>
              <Field
                name="email"
                type="email"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
              />
              {errors.email && touched.email && (
                <div className="text-red-500 text-sm mt-1">{errors.email}</div>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
            >
              {isSubmitting ? 'Đang xử lý...' : 'Đăng ký'}
            </button>
          </Form>
        )}
      </Formik>
    </div>
  );
};

export default RegistrationForm;
